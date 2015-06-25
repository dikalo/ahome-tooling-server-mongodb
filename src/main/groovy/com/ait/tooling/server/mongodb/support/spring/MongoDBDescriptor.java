/*
 * Copyright (c) 2014,2015 Ahome' Innovation Technologies. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ait.tooling.server.mongodb.support.spring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.log4j.Logger;

import com.ait.tooling.common.api.java.util.StringOps;
import com.ait.tooling.common.api.types.Activatable;
import com.ait.tooling.server.core.support.spring.IPropertiesResolver;
import com.ait.tooling.server.core.support.spring.ServerContextInstance;
import com.ait.tooling.server.mongodb.MongoDB;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class MongoDBDescriptor extends Activatable implements IMongoDBDescriptor
{
    private static final long          serialVersionUID = 4921229488618930502L;

    private static final Logger        logger           = Logger.getLogger(MongoDBDescriptor.class);

    private String                     m_name;

    private MongoDB                    m_mongo_db;

    private boolean                    m_createid       = false;

    private boolean                    m_replicas       = false;

    private int                        m_poolsize       = 100;

    private int                        m_multiple       = 100;

    private int                        m_ctimeout       = 10000;

    private String                     m_defaultd;

    private MongoClientOptions         m_coptions;

    private ArrayList<ServerAddress>   m_addrlist;

    private ArrayList<MongoCredential> m_authlist;

    private final String               m_baseprop;

    public MongoDBDescriptor(final String baseprop)
    {
        m_baseprop = StringOps.requireTrimOrNull(baseprop);
    }

    @Override
    public boolean setActive(final boolean active)
    {
        if (null == m_addrlist)
        {
            if (false == init())
            {
                return false;
            }
        }
        return super.setActive(active);
    }

    private final boolean init()
    {
        try
        {
            final IPropertiesResolver prop = ServerContextInstance.getServerContextInstance().getPropertiesResolver();

            setName(prop.getPropertyByName(m_baseprop + ".name"));

            setDefaultDB(prop.getPropertyByName(m_baseprop + ".db"));

            setReplicas(Boolean.valueOf(prop.getPropertyByName(m_baseprop + ".replicas", "false")));

            setCreateID(Boolean.valueOf(prop.getPropertyByName(m_baseprop + ".createid", "false")));

            final ArrayList<ServerAddress> addrlist = new ArrayList<ServerAddress>();

            for (String name : StringOps.requireTrimOrNull(prop.getPropertyByName(m_baseprop + ".host.list")).split(","))
            {
                name = StringOps.toTrimOrNull(name);

                if (null != name)
                {
                    final String addr = StringOps.requireTrimOrNull(prop.getPropertyByName(m_baseprop + ".host." + name + ".addr"));

                    final String port = StringOps.requireTrimOrNull(prop.getPropertyByName(m_baseprop + ".host." + name + ".port"));

                    addrlist.add(new ServerAddress(addr, Integer.valueOf(port)));
                }
            }
            if (addrlist.isEmpty())
            {
                throw new IllegalArgumentException("no MongoDB server address");
            }
            m_addrlist = addrlist;

            final String temp = StringOps.toTrimOrNull(prop.getPropertyByName(m_baseprop + ".auth.list"));

            if (null != temp)
            {
                final ArrayList<MongoCredential> authlist = new ArrayList<MongoCredential>();

                for (String name : temp.split(","))
                {
                    name = StringOps.toTrimOrNull(name);

                    if (null != name)
                    {
                        final String user = StringOps.requireTrimOrNull(prop.getPropertyByName(m_baseprop + ".auth." + name + ".user"));

                        final String pass = StringOps.requireTrimOrNull(prop.getPropertyByName(m_baseprop + ".auth." + name + ".pass"));

                        final String data = StringOps.requireTrimOrNull(prop.getPropertyByName(m_baseprop + ".auth." + name + ".db"));

                        authlist.add(MongoCredential.createCredential(user, data, pass.toCharArray()));
                    }
                }
                m_authlist = authlist;
            }
            if (null == getClientOptions())
            {
                setClientOptions(MongoClientOptions.builder().connectionsPerHost(getConnectionPoolSize()).threadsAllowedToBlockForConnectionMultiplier(getConnectionMultiplier()).connectTimeout(getConnectionTimeout()).build());
            }
            return true;
        }
        catch (Exception e)
        {
            logger.error("error initializing MongoDBDescriptor", e);

            return false;
        }
    }

    @Override
    public boolean isCreateID()
    {
        return m_createid;
    }

    private final void setCreateID(final boolean createid)
    {
        m_createid = createid;
    }

    @Override
    public void close() throws IOException
    {
        if (null != m_mongo_db)
        {
            m_mongo_db.close();
        }
    }

    @Override
    public String getName()
    {
        return m_name;
    }

    private final void setName(final String name)
    {
        m_name = Objects.requireNonNull(StringOps.toTrimOrNull(name), "MongoDBDescriptor name is null or empty");
    }

    @Override
    public synchronized MongoDB getMongoDB()
    {
        if (null == m_mongo_db)
        {
            m_mongo_db = new MongoDB(getAddresses(), getCredentials(), getClientOptions(), isReplicas(), getDefaultDB(), isCreateID());
        }
        return m_mongo_db;
    }

    @Override
    public int getConnectionTimeout()
    {
        return m_ctimeout;
    }

    @Override
    public int getConnectionMultiplier()
    {
        return m_multiple;
    }

    @Override
    public int getConnectionPoolSize()
    {
        return m_poolsize;
    }

    public void setConnectionTimeout(final int timeout)
    {
        m_ctimeout = Math.max(0, timeout);
    }

    public void setConnectionMultiplier(final int multiplier)
    {
        m_multiple = Math.max(0, multiplier);
    }

    public void setConnectionPoolSize(final int poolsize)
    {
        m_poolsize = Math.max(1, poolsize);
    }

    @Override
    public String getDefaultDB()
    {
        return m_defaultd;
    }

    public void setDefaultDB(final String name)
    {
        m_defaultd = Objects.requireNonNull(StringOps.toTrimOrNull(name), "DefaultDB is null or empty");
    }

    @Override
    public List<MongoCredential> getCredentials()
    {
        return Collections.unmodifiableList(m_authlist);
    }

    @Override
    public boolean isReplicas()
    {
        return m_replicas;
    }

    public void setReplicas(final boolean replicas)
    {
        m_replicas = replicas;
    }

    @Override
    public List<ServerAddress> getAddresses()
    {
        return Collections.unmodifiableList(m_addrlist);
    }

    public void setClientOptions(final MongoClientOptions coptions)
    {
        m_coptions = Objects.requireNonNull(coptions);
    }

    @Override
    public MongoClientOptions getClientOptions()
    {
        return m_coptions;
    }
}

/*
   Copyright (c) 2014,2015 Ahome' Innovation Technologies. All rights reserved.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.ait.tooling.server.mongodb.support.spring;

import org.springframework.core.env.Environment;
import org.springframework.web.context.WebApplicationContext;

import com.ait.tooling.server.core.jmx.management.IServerManager;
import com.ait.tooling.server.core.security.IAuthorizationProvider;
import com.ait.tooling.server.core.support.spring.IPropertiesProvider;
import com.ait.tooling.server.core.support.spring.IServerContext;
import com.ait.tooling.server.core.support.spring.ServerContextInstance;

public final class MongoDBContextInstance implements IMongoDBContext
{
    private static final MongoDBContextInstance INSTANCE = new MongoDBContextInstance();

    public static final MongoDBContextInstance get()
    {
        return INSTANCE;
    }
    
    private MongoDBContextInstance()
    {
    }

    @Override
    public IMongoDBProvider getMongoDBProvider()
    {
        return getBean("MongoDBProvider", IMongoDBProvider.class);
    }

    @Override
    public IServerContext getServerContext()
    {
        return ServerContextInstance.get();
    }

    @Override
    public WebApplicationContext getApplicationContext()
    {
        return getServerContext().getApplicationContext();
    }

    @Override
    public Environment getEnvironment()
    {
        return getServerContext().getEnvironment();
    }

    @Override
    public <T> T getBean(String name, Class<T> type)
    {
        return getServerContext().getBean(name, type);
    }

    @Override
    public IPropertiesProvider getPropertiesProvider()
    {
        return getServerContext().getPropertiesProvider();
    }

    @Override
    public String getPropertyByName(String name)
    {
        return getServerContext().getPropertyByName(name);
    }

    @Override
    public String getPropertyByName(String name, String otherwise)
    {
        return getServerContext().getPropertyByName(name, otherwise);
    }

    @Override
    public IAuthorizationProvider getAuthorizationProvider()
    {
        return getServerContext().getAuthorizationProvider();
    }

    @Override
    public Iterable<String> getPrincipalsKeys()
    {
        return getServerContext().getPrincipalsKeys();
    }

    @Override
    public IServerManager getServerManager()
    {
        return getServerContext().getServerManager();
    }
}
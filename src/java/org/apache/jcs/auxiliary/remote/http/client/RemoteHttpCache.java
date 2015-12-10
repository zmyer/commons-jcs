package org.apache.jcs.auxiliary.remote.http.client;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.jcs.auxiliary.remote.AbstractRemoteAuxiliaryCache;
import org.apache.jcs.auxiliary.remote.ZombieRemoteCacheService;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheListener;
import org.apache.jcs.auxiliary.remote.behavior.IRemoteCacheService;

/**
 * This uses an http client as the service.
 */
public class RemoteHttpCache<K extends Serializable, V extends Serializable>
    extends AbstractRemoteAuxiliaryCache<K, V>
{
    /** Don't change. */
    private static final long serialVersionUID = -5329231850422826461L;

    /** The logger. */
    private final static Log log = LogFactory.getLog( RemoteHttpCache.class );

    /** Keep the child copy here for the restore process. */
    private RemoteHttpCacheAttributes remoteHttpCacheAttributes;

    /**
     * Constructor for the RemoteCache object. This object communicates with a remote cache server.
     * One of these exists for each region. This also holds a reference to a listener. The same
     * listener is used for all regions for one remote server. Holding a reference to the listener
     * allows this object to know the listener id assigned by the remote cache.
     * <p>
     * @param remoteHttpCacheAttributes
     * @param remote
     * @param listener
     */
    public RemoteHttpCache( RemoteHttpCacheAttributes remoteHttpCacheAttributes, IRemoteCacheService<K, V> remote,
                            IRemoteCacheListener<K, V> listener )
    {
        super( remoteHttpCacheAttributes, remote, listener );

        setRemoteHttpCacheAttributes( remoteHttpCacheAttributes );
    }

    /**
     * Nothing right now. This should setup a zombie and initiate recovery.
     * <p>
     * @param ex
     * @param msg
     * @param eventName
     * @throws IOException
     */
    protected void handleException( Exception ex, String msg, String eventName )
        throws IOException
    {
        // we should not switch if the existing is a zombie.
        if ( !( getRemoteCacheService() instanceof ZombieRemoteCacheService ) )
        {
            String message = "Disabling remote cache due to error: " + msg;
            logError( cacheName, "", message );
            log.error( message, ex );

            setRemoteCacheService( new ZombieRemoteCacheService<K, V>( getRemoteCacheAttributes().getZombieQueueMaxSize() ) );

            RemoteHttpCacheMonitor.getInstance().notifyError( this );
        }

        if ( ex instanceof IOException )
        {
            throw (IOException) ex;
        }
        throw new IOException( ex.getMessage() );
    }

    /**
     * @return url of service
     */
    public String getEventLoggingExtraInfo()
    {
        return null;
    }

    /**
     * @param remoteHttpCacheAttributes the remoteHttpCacheAttributes to set
     */
    public void setRemoteHttpCacheAttributes( RemoteHttpCacheAttributes remoteHttpCacheAttributes )
    {
        this.remoteHttpCacheAttributes = remoteHttpCacheAttributes;
    }

    /**
     * @return the remoteHttpCacheAttributes
     */
    public RemoteHttpCacheAttributes getRemoteHttpCacheAttributes()
    {
        return remoteHttpCacheAttributes;
    }
}
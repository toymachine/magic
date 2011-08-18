
package org.openid4java.appengine;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.consumer.AbstractNonceVerifier;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * @author Henk Punt
 */
public class AppEngineNonceVerifier extends AbstractNonceVerifier
{
    private static Log _log = LogFactory.getLog(AppEngineNonceVerifier.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private MemcacheService _cache;
    
    public AppEngineNonceVerifier(int maxAge)
    { 
        super(maxAge);
        
    	_cache = MemcacheServiceFactory.getMemcacheService("AppEngineNonceVerifier");   
    }

    protected int seen(Date now, String opUrl, String nonce)
    {
        String pair = opUrl + '#' + nonce;

        if (_cache.get(pair) != null)
        {
            _log.error("Possible replay attack! Already seen nonce: " + nonce);
            return SEEN;
        }

        _cache.put(pair, "SET", Expiration.byDeltaSeconds(_maxAgeSeconds));

        if (DEBUG) _log.debug("Nonce verified: " + nonce);

        return OK;
    }
}

package org.openid4java.appengine;

import java.sql.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerAssociationStore;

public class AppEngineConsumerAssociationStore implements ConsumerAssociationStore {

    private static Log _log = LogFactory.getLog(AppEngineConsumerAssociationStore.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

	public Association load ( String opUrl, String handle )
    {               
		/*

		try {
                  String type = (String) res.get ( "type" ) ;
                  String macKey = (String) res.get ( "mackey" ) ;
                  Date expDate = (Date) res.get ( "expdate" ) ;

                    if ( type == null || macKey == null || expDate == null )
                            throw new AssociationException (
                                                                                                    "Invalid association data retrived from database; cannot create Association "
                                                                                                                    + "object for handle: "
                                                                                                                    + handle ) ;

                    Association assoc ;

                    if ( Association.TYPE_HMAC_SHA1.equals ( type ) )
                            assoc = Association.createHmacSha1 (    handle,
                                                                                                            Base64.decodeBase64 ( macKey.getBytes ( ) ),
                                                                                                            expDate ) ;

                    else if ( Association.TYPE_HMAC_SHA256.equals ( type ) )
                            assoc = Association.createHmacSha256 (  handle,
                                                                                                            Base64.decodeBase64 ( macKey.getBytes ( ) ),
                                                                                                            expDate ) ;

                    else
                            throw new AssociationException (
                                                                                                    "Invalid association type "
                                                                                                                    + "retrieved from database: "
                                                                                                                    + type ) ;

                    if ( _log.isDebugEnabled ( ) )
                            _log.debug ( "Retrieved association for handle: " + handle
                                                            + " from table: " + _tableName ) ;

                    return assoc ;
            }
            catch ( Exception dae )
            {
            	//LOG
                return null ;
            }
            */
		return null;
    }
	
	@Override
	public void remove(String arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void save(String arg0, Association arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Association load(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}

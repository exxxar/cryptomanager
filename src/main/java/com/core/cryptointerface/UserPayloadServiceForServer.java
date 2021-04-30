package com.core.cryptointerface;

import com.core.cryptointerface.components.Settings;
import com.core.cryptolib.factories.RequestPrepareFactory;
import com.core.cryptolib.forms.HandlerResultForm;
import com.core.cryptolib.forms.TransferDataForm;
import com.core.cryptolib.forms.TransferForm;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public class UserPayloadServiceForServer extends com.core.cryptolib.UserPayloadServiceForServer {

    String microserviceURL;

    RequestPrepareFactory rpf;

    public UserPayloadServiceForServer(Settings settings) {
        super(settings);
        rpf = new RequestPrepareFactory(this.getSettings());
        microserviceURL = settings.get("microserviceUrl").getValue();
    }

    private Object requestPOST(JSONObject form, String link, String apiVersion) throws MalformedURLException, ParseException, UnsupportedEncodingException {
        JSONObject result = null;
        try {

            result = rpf.jsonPost(microserviceURL + link,
                    Optional.of(apiVersion),
                    form
            );

        } catch (IOException ex) {
            Logger.getLogger(UserPayloadServiceForServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (result == null) {
            return null;
        }

        TransferDataForm tdf = new TransferDataForm();
        tdf.setData((String) result.get("data"));
        tdf.setType(Integer.parseInt((String) result.get("type")));

        return tdf;
    }

    private Object requestGET(String link, String apiVersion) throws MalformedURLException, ParseException, UnsupportedEncodingException {
        JSONObject result = null;
        try {

            result = rpf.jsonGet(microserviceURL + link,
                    Optional.of(apiVersion)
            );
        } catch (IOException ex) {
            Logger.getLogger(UserPayloadServiceForServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (result == null) {
            return null;
        }

        TransferDataForm tdf = new TransferDataForm();
        tdf.setData((String) result.get("data"));
        tdf.setType(Integer.parseInt((String) result.get("type")));

        return tdf;
    }

    public TransferDataForm getTrustedDevicePublicId() throws ParseException, UnsupportedEncodingException, MalformedURLException {
        return (TransferDataForm) requestGET("/cryptolib/server/getTrustedDevicePublicId", "0.0.3");
    }

    public TransferDataForm onceEncryptedRequest() throws ParseException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, InvalidKeySpecException, MalformedURLException {
        return (TransferDataForm) requestGET("/cryptolib/server/onceEncryptedRequest", "0.0.3");

    }

    @Override
    public HandlerResultForm handler(TransferForm transfer) throws ParseException, UnsupportedEncodingException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException, IOException {

        return (HandlerResultForm) requestPOST(transfer.toJSON(), "/cryptolib/server/handler", "0.0.3");

    }

    public TransferDataForm twiceEncryptedRequest(TransferDataForm tdfIncoming) throws ParseException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, InvalidKeySpecException, MalformedURLException {
        return (TransferDataForm) requestPOST(tdfIncoming.toJSON(), "/cryptolib/server/twiceEncryptedRequest", "0.0.3");
    }

    public TransferDataForm twiceEncryptedPermission(TransferDataForm incomingTransferDataForm) throws ParseException, NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, InvalidAlgorithmParameterException, UnsupportedEncodingException, InvalidKeySpecException, MalformedURLException {

        return (TransferDataForm) requestPOST(incomingTransferDataForm.toJSON(), "/cryptolib/server/twiceEncryptedPermission", "0.0.3");

    }

    @Override
    public TransferDataForm dataRequest(TransferDataForm incomingTransferDataForm) throws ParseException, InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        try {
            return (TransferDataForm) requestPOST(incomingTransferDataForm.toJSON(), "/cryptolib/server/dataRequest", "0.0.3");
        } catch (MalformedURLException ex) {
            Logger.getLogger(UserPayloadServiceForServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    @Override
    public TransferDataForm encryptedDataRequest(
            String trustedDeviceData,
            TransferDataForm incomingTransferDataForm) throws ParseException, UnsupportedEncodingException {
        try {
            return (TransferDataForm) requestPOST(incomingTransferDataForm.toJSON(), "/cryptolib/server/encryptedDataRequest/" + trustedDeviceData, "0.0.3");
        } catch (MalformedURLException ex) {
            Logger.getLogger(UserPayloadServiceForServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

}

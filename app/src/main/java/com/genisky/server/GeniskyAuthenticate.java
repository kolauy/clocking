package com.genisky.server;

import com.genisky.account.AuthenticationRequest;
import com.genisky.account.AuthenticationResponse;

public class GeniskyAuthenticate extends ServerConnection {

    public GeniskyAuthenticate(String server){
        super("", server);
    }

    public AuthenticationResponse register(AuthenticationRequest request){
        String entity = _gson.toJson(request);
        return sendPost("/authenticate", entity, AuthenticationResponse.class);
    }
}


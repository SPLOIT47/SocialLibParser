package com.sociallibparser.singelton;

import com.sociallibparser.manager.RequestManager;

public class RequestManagerSigelton {
    private static RequestManagerSigelton INSTANCE;
    private final RequestManager requestManager;

    public RequestManagerSigelton() {
        requestManager = new RequestManager();
    }

    public synchronized static RequestManagerSigelton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RequestManagerSigelton();
        }

        return INSTANCE;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }
}

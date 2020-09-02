package com.parashift.onlyoffice;

import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.UrlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Base64;

/*
   Copyright (c) Ascensio System SIA 2020. All rights reserved.
   http://www.onlyoffice.com
*/

@Service
public class Util {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    @Autowired
    @Qualifier("checkOutCheckInService")
    CheckOutCheckInService cociService;

    @Autowired
    AuthenticationService authenticationService;

    @Autowired
    VersionService versionService;

    @Autowired
    SysAdminParams sysAdminParams;

    @Autowired
    NodeService nodeService;

    @Autowired
    ConfigManager configManager;

    public static final QName EditingKeyAspect = QName.createQName("onlyoffice:editing-key");
    public static final QName EditingHashAspect = QName.createQName("onlyoffice:editing-hash");

    public String getKey(NodeRef nodeRef) {
        String key = null;
        if (cociService.isCheckedOut(nodeRef)) {
            key = (String) nodeService.getProperty(cociService.getWorkingCopy(nodeRef), EditingKeyAspect);
        }

        if (key == null) {
            Version v = versionService.getCurrentVersion(nodeRef);
            key = nodeRef.getId() + "_" + v.getVersionLabel();
        }

        return key;
    }

    public String getHash(NodeRef nodeRef) {
        String hash = null;
        if (cociService.isCheckedOut(nodeRef)) {
            hash = (String) nodeService.getProperty(cociService.getWorkingCopy(nodeRef), EditingHashAspect);
        }
        return hash;
    }

    public String getContentUrl(NodeRef nodeRef) {
        return  getAlfrescoUrl() + "s/api/node/content/workspace/SpacesStore/" + nodeRef.getId() + "?alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getCallbackUrl(NodeRef nodeRef) {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/callback?nodeRef=" + nodeRef.toString() + "&cb_key=" + getHash(nodeRef);
    }

    public String getConversionUrl(String key) {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/converter?key=" + key + "&alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getTestConversionUrl() {
        return getAlfrescoUrl() + "s/parashift/onlyoffice/convertertest?alf_ticket=" + authenticationService.getCurrentTicket();
    }

    public String getEditorUrl() {
        return (String) configManager.getOrDefault("url", "http://127.0.0.1/");
    }

    public String getEditorInnerUrl() {
        String url = (String) configManager.getOrDefault("innerurl", "");
        if (url.isEmpty()) {
            return getEditorUrl();
        } else {
            return url;
        }
    }

    public String generateHash() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[32];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private String getAlfrescoUrl() {
        String alfUrl = (String) configManager.getOrDefault("alfurl", "");
        if (alfUrl.isEmpty()) {
            return UrlUtil.getAlfrescoUrl(sysAdminParams) + "/";
        } else {
            return alfUrl + "alfresco/";
        }
    }
}

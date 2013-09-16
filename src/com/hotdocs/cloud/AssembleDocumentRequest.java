/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class AssembleDocumentRequest extends Request {

    private OutputFormat format = OutputFormat.Native;
    private Map<String, String> settings = new HashMap<String, String>();

    public AssembleDocumentRequest(String packageId, String packageFile) {
        super(packageId, packageFile);
    }

    public AssembleDocumentRequest(
            String packageId,
            String packageFile,
            String billingRef,
            String templateName,
            String answers,
            OutputFormat format) {

        super(packageId, packageFile, answers, billingRef);
        this.templateName = templateName;
        this.format = format;
    }
    
    public void setSetting(String name, String value) {
        settings.put(name, value);
    }

    @Override
    String getPathPrefix() {
        return "/hdcs/assemble";
    }

    @Override
    String getQuery() {
        StringBuilder buffer = new StringBuilder();

        buffer.append("format=");
        buffer.append(format.name());

        for (Entry<String, String> setting : settings.entrySet()) {
            buffer.append("&");
            buffer.append(setting.getKey());
            buffer.append("=");
            buffer.append(setting.getValue());
        }

        return buffer.toString();
    }

    @Override
    String getMethod() {
        return "POST";
    }

    @Override
    Collection<Object> getHmacParams() {
        return Arrays.asList(
                packageId,
                templateName,
                new Boolean(false),
                billingRef,
                format,
                settings);
    }

}

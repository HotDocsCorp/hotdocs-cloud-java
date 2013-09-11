/* Copyright (c) 2013, HotDocs Limited
   Use, modification and redistribution of this source is subject
   to the New BSD License as set out in LICENSE.TXT. */

package com.hotdocs.cloud;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class GetInterviewRequest extends Request {
    
    private InterviewFormat format = InterviewFormat.JavaScript;
    private String[] markedVariables;
    private String tempImageUrl;
    private Map<String, String> settings = new HashMap<String, String>();
    
    public GetInterviewRequest(String packageId, String packageFile) {
        super(packageId, packageFile);
    }
    
    public GetInterviewRequest(
        String packageId,
        String packageFile,
        String billingRef,
        String templateName,
        String answers,
        InterviewFormat format,
        String[] markedVariables,
        String tempImageUrl) {
        
        super(packageId, packageFile, answers, billingRef);
        this.templateName = templateName;
        this.format = format;
        this.markedVariables = markedVariables;
        this.tempImageUrl = tempImageUrl;
    }

    @Override
    String getPathPrefix() {
        return "/hdcs/interview";
    }

    @Override
    String getQuery() {
        StringBuilder buffer = new StringBuilder();
        
        buffer.append("format=");
        buffer.append(format.name());
        
        if (markedVariables != null && markedVariables.length > 0) {
            buffer.append("&markedvariables=");
            buffer.append(Util.join(markedVariables, ","));
        }
        
        if (!Util.isNullOrEmpty(tempImageUrl)) {
            buffer.append("&tempimageurl=");
            buffer.append(tempImageUrl);
        }
        
        if (settings != null) {
            for (Entry<String,String> setting : settings.entrySet()) {
                buffer.append("&");
                buffer.append(setting.getKey());
                buffer.append("=");
                buffer.append(setting.getValue());
            }
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
                tempImageUrl,
                settings);
    }

}

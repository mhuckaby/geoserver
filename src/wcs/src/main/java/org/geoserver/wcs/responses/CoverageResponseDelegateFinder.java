/* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geoserver.wcs.responses;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import org.geoserver.platform.GeoServerExtensions;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Locates the encoders for a certain coverage format
 * 
 * @author Alessio Fabiani (alessio.fabiani@gmail.com) $ (last modification)
 * @author $Author: Simone Giannecchini (simboss1@gmail.com) $ (last modification)
 */
public class CoverageResponseDelegateFinder implements ApplicationContextAware {
    
    private ApplicationContext applicationContext;
    
    private CoverageResponseDelegateFinder() {
    }

    /**
     * Locates an encoder for a specific GetCoverage results output format
     */
    public CoverageResponseDelegate encoderFor(String outputFormat) {

        // lookup the encoders dynamically (the list returned might be subject to dynamic extension filters, so we don't cache it)
        List<CoverageResponseDelegate> delegates = GeoServerExtensions.extensions(CoverageResponseDelegate.class, applicationContext);
        for (CoverageResponseDelegate delegate : delegates) {
            if (delegate.isAvailable() && delegate.canProduce(outputFormat)) {
                try {
                    if (delegate != null) {
                        return delegate.getClass().newInstance();
                    }
                } catch (IllegalAccessException ex) {
                    final NoSuchElementException e = new NoSuchElementException(new StringBuffer(
                            "Can't create the encoder ").append(delegate.getClass().getName())
                            .toString());
                    e.initCause(ex);
                    throw e;
                } catch (InstantiationException ex) {
                    final NoSuchElementException e = new NoSuchElementException(new StringBuffer(
                            "Can't create the encoder ").append(delegate.getClass().getName())
                            .toString());
                    e.initCause(ex);
                    throw e;
                }
            }
        }

        return null;
    }
    
    /**
     * Returns the list of all the supported output formats
     * @return
     */
    public List<String> getOutputFormats() {
        Set<String> formats = new HashSet<String>();
        List<CoverageResponseDelegate> delegates = GeoServerExtensions.extensions(CoverageResponseDelegate.class, applicationContext);
        for (CoverageResponseDelegate delegate : delegates) {
            if(delegate.isAvailable()) {
                formats.addAll(delegate.getOutputFormats());
            }
        }
        
        List<String> result = new ArrayList<String>(formats);
        Collections.sort(result);
        return result;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;        
    }

}

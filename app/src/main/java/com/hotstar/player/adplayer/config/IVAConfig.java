/*******************************************************************************
 * ADOBE CONFIDENTIAL
 *  ___________________
 *
 *  Copyright 2015 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 *  NOTICE:  All information contained herein is, and remains
 *  the property of Adobe Systems Incorporated and its suppliers,
 *  if any.  The intellectual and technical concepts contained
 *  herein are proprietary to Adobe Systems Incorporated and its
 *  suppliers and are protected by trade secret or copyright law.
 *  Dissemination of this information or reproduction of this material
 *  is strictly forbidden unless prior written permission is obtained
 *  from Adobe Systems Incorporated.
 ******************************************************************************/

package com.hotstar.player.adplayer.config;

public interface IVAConfig {

    /**
     * Determines if the Video Analytics module is enabled.
     *
     * @return true if the Video Analytics module is enabled, false otherwise.
     */
    public  boolean isVAEnabled();

    /**
     * Determines if debug tracing should be activated. It is recommended to turn this off for production version
     * as this will activate pretty extensive tracing messaging
     *
     * @return true if so, false otherwise
     */
    public boolean isVADebugLoggingEnabled();

    /**
     * Determines if video tracking quiet mode is activated. In this mode,  no network call are sent over the
     * network at all.
     *
     * @return true if so, false otherwise
     */
    public boolean isVAQuietModeEnabled();

    /**
     * Get the Video Analytics tracking end-point. This is where all the HTTP calls are sent
     *
     * @return String value of the end-point URL
     */
    public String getVATrackingServer();

    /**
     * Get the Video Analytics job ID. It is an indicator for the back-end end-point about what kind of
     * processing should be applied for the video-tracking calls. This value is provided by Adobe in advance.
     *
     * @return String value of job ID
     */
    public String getVAJobId();

    /**
     * Get the Video Analytics content publisher. This value is provided by Adobe in advance.
     *
     * @return String value of publisher
     */
    public String getVAPublisher();

    /**
     * Get the name of the channel where the user is watchin the content.
     * For a mobile app, the name of the app.
     * For a website, the domain name.
     *
     * @return name of heartbeat channel
     */
    public String getVAChannel();

}

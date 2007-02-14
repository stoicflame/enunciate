package com.ifyouwannabecool.api;

import javax.xml.ws.WebFault;

/**
 * Thrown when an attempt is made to do something without the correct permissions.
 *
 * @author Ryan Heaton
 */
@WebFault
public class PermissionDeniedException extends Exception {
}

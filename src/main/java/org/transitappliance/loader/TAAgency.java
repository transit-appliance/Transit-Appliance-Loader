/*
Copyright 2012 Portland Transport

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.transitappliance.loader;

import java.util.HashMap;

/**
 * Defines an agency, ready for serialization
 */
public class TAAgency {
    public String id;
    public String agency_lang;
    public String agency_name;
    // TODO: Not a String
    public String agency_timezone;
    public String agency_url;
    public String avl_agency_id;
    public String avl_service;
    public String rights_notice;
    public HashMap<String, String> avl_options = new HashMap<String, String>();
}
    
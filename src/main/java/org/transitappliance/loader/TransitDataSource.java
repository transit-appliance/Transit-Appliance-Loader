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

/**
 * This defines a source for transit data. It could be a GTFS file, API, &c.
 */
public abstract class TransitDataSource {
    /**
     * Return the next stop from this datasource, or null if there are no more stops.
     */
    public abstract TAStop getStop ();

    /**
     * Initialize the data source.
     */
    public void initialize () {
        // not abstract; some data sources require no initx
    };
}
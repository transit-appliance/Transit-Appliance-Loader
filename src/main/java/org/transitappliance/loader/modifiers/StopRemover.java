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

package org.transitappliance.loader.modifiers;

import java.util.List;
import org.transitappliance.loader.StopModifier;
import org.transitappliance.loader.TAStop;

/**
 * Remove the stops with IDs specified in stopsToRemove
 */
public class StopRemover implements StopModifier {
    // spring configured
    private List<String> stopsToRemove;
    public void setStopsToRemove (List<String> stopsToRemove) {
        this.stopsToRemove = stopsToRemove;
    }

    /**
     * Do the removal of stops.
     * @param stop The stop to process
     */
    public TAStop processStop (TAStop stop) {
        // if it's been marked for removal, remove it
        if (stopsToRemove.contains(stop.stop_id))
            return null;
        
        else return stop;
    }
}
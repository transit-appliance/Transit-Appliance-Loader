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
import java.util.ArrayList;
import org.transitappliance.loader.StopModifier;
import org.transitappliance.loader.TAStop;
import org.transitappliance.loader.TARoute;

/**
 * Retain only routes of the specified type, and their respective stops.
 */
public class RouteTypeRetainer implements StopModifier {
    // spring configured
    private int routeType;
    public void setRouteType (int routeType) {
        this.routeType = routeType;
    }

    /**
     * Do the removal of routes and stops.
     * @param stop The stop to process
     */
    public TAStop processStop (TAStop stop) {
        // loop over routes and save ones that are needed
        ArrayList<TARoute> routesOut = new ArrayList<TARoute>();
        for (TARoute route : stop.routes) {
            if (route.route_type == routeType)
                routesOut.add(route);
        }
        
        // If we've removed all routes, remove the stop too
        if (routesOut.size() == 0)
            return null;

        // overwrite the routes
        stop.routes = routesOut;
        
        return stop;
    }
}
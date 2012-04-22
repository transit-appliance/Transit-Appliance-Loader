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
 * Classes implementing this interface can be used to modify stops (including setting avl_stop_ids).
 */
public interface StopModifier {
   /**
    * Process the stop, making any modifications necessary. The stop can be removed by
    * returning null
    */
   public TAStop processStop(TAStop stop);
}
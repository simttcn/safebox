/**
 * Designed and developed by Aidan Follestad (@smttcn)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.smttcn.materialdialogs

/** @author Aidan Follestad (smttcn) */
enum class LayoutMode {
  /** The layout height fills in the screen. */
  MATCH_PARENT,
  /**
   * The layout height wraps its children, maxing out at the screen height in which case the
   * children should be inside of a ScrollView.
   */
  WRAP_CONTENT
}

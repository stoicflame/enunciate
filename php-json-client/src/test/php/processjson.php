<?php
/*
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
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

  include('api-php-json-client.php');

  // Capture the arguments
  $classname = str_replace("::", "\\", $argv[1]);
  $infile = $argv[2];
  $outfile = $argv[3];

  // Load json from a file
  $filecontents = file_get_contents($infile);
  
  // Deserialize to an object
  $parsed = json_decode($filecontents, true);
  eval("\$o = new $classname();");
  $o->initFromArray($parsed);
  
  // Serialize object to json
  file_put_contents($outfile, $o->toJson());

?>
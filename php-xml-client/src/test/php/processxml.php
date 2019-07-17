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

  include('api-php-xml-client.php');

  // Capture the arguments
  $classname = str_replace("::", "\\", $argv[1]);
  $infile = $argv[2];
  $outfile = $argv[3];

  // Deserialize to an object
  $xml = new XMLReader();
  if (!$xml->open($infile)) {
    throw new \Exception('Unable to open ' . $infile . '/sample-gx.xml');
  }
  eval("\$o = new $classname(\$xml);");

  $writer = new \XMLWriter();
  $writer->openUri($outfile);
  $writer->startDocument();
  $writer->setIndent(4);
  $o->toXml($writer);
  $writer->flush();

?>
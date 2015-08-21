<?php

  include('enunciate.php');

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
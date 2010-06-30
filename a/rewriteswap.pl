#! /usr/bin/perl -w

$file = $ARGV[0];

$rewritten = $file . ".rewritten";

$orig = $file . ".orig";

system(("java be.ac.kuleuven.cs.ttm.transformer.Transformer $file > /dev/null"));
system(("cp $file $orig"));
system(("cp $rewritten $file"));


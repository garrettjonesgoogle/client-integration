/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.combiner;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;

/**
 * An example that counts words in Shakespeare.
 *
 * <p>No arguments are required to run this pipeline. It will be executed with the DirectRunner. You
 * can see the results in the output files in your current working directory, with names like
 * "wordcounts-00001-of-00005. When running on a distributed service, you would use an appropriate
 * file service.
 */
public class DataflowHello {
  public static final String TOKENIZER_PATTERN = "[^\\p{L}]+";

  public static void doHello() {
    System.out.println("=== Dataflow: minimal word count ===");

    // Create a PipelineOptions object. This object lets us set various execution
    // options for our pipeline, such as the runner you wish to use. This example
    // will run with the DirectRunner by default, based on the class path configured
    // in its dependencies.
    PipelineOptions options = PipelineOptionsFactory.create();

    // Create the Pipeline object with the options we defined above.
    Pipeline p = Pipeline.create(options);

    // Apply the pipeline's transforms.

    // Concept #1: Apply a root transform to the pipeline; in this case, TextIO.Read to read a set
    // of input text files. TextIO.Read returns a PCollection where each element is one line from
    // the input text (a set of Shakespeare's texts).

    // This example reads a public data set consisting of the complete works of Shakespeare.
    p.apply(TextIO.read().from("gs://apache-beam-samples/shakespeare/*"))

        // Concept #2: Apply a ParDo transform to our PCollection of text lines. This ParDo invokes a
        // DoFn (defined in-line) on each element that tokenizes the text line into individual words.
        // The ParDo returns a PCollection<String>, where each element is an individual word in
        // Shakespeare's collected texts.
        .apply("ExtractWords", ParDo.of(new DoFn<String, String>() {
          @ProcessElement
          public void processElement(ProcessContext c) {
            for (String word : c.element().split(TOKENIZER_PATTERN)) {
              if (!word.isEmpty()) {
                c.output(word);
              }
            }
          }
        }))

        // Concept #3: Apply the Count transform to our PCollection of individual words. The Count
        // transform returns a new PCollection of key/value pairs, where each key represents a unique
        // word in the text. The associated value is the occurrence count for that word.
        .apply(Count.<String>perElement())

        // Apply a MapElements transform that formats our PCollection of word counts into a printable
        // string, suitable for writing to an output file.
        .apply("FormatResults", MapElements.via(new SimpleFunction<KV<String, Long>, String>() {
          @Override
          public String apply(KV<String, Long> input) {
            return input.getKey() + ": " + input.getValue();
          }
        }))

        // Concept #4: Apply a write transform, TextIO.Write, at the end of the pipeline.
        // TextIO.Write writes the contents of a PCollection (in this case, our PCollection of
        // formatted strings) to a series of text files.
        //
        // By default, it will write to a set of files with names like wordcount-00001-of-00005
        .apply(TextIO.write().to("wordcounts"));

    System.out.println("Pipeline started; now waiting for it to finish.");

    // Run the pipeline.
    p.run().waitUntilFinish();

    System.out.println("Pipeline done!");
  }
}

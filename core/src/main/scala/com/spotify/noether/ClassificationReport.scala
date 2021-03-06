/*
 * Copyright 2018 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.noether
import com.twitter.algebird.{Aggregator, Semigroup}

final case class Scores(mcc: Double,
                        fscore: Double,
                        precision: Double,
                        recall: Double,
                        accuracy: Double,
                        fpr: Double)

final case class ClassificationAggregator(threshold: Double = 0.5, beta: Double = 1.0)
  extends Aggregator[Prediction[Boolean, Double], Map[(Int, Int), Long], Scores] {

  private val aggregator = ConfusionMatrix(Seq(0, 1))

  def prepare(input: Prediction[Boolean, Double]): Map[(Int, Int), Long] = {
    val predicted = Prediction(if(input.actual) 1 else 0, if(input.predicted > threshold) 1 else 0)
    aggregator.prepare(predicted)
  }

  def semigroup: Semigroup[Map[(Int, Int), Long]] = aggregator.semigroup

  def present(m: Map[(Int, Int), Long]): Scores = {
    val mat = aggregator.present(m)

    val fp = mat(1, 0).toDouble
    val tp = mat(1, 1).toDouble
    val tn = mat(0, 0).toDouble
    val fn = mat(0, 1).toDouble

    val mccDenom = math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn))
    val mcc = if(mccDenom > 0.0) ((tp * tn) - (fp * fn)) / mccDenom else 0.0

    val precDenom = tp + fp
    val precision = if(precDenom > 0.0) tp / precDenom else 1.0

    val recallDenom = tp + fn
    val recall = if(recallDenom > 0.0) tp / recallDenom else 1.0

    val accuracyDenom = tp + fn + tn + fp
    val accuracy = if(accuracyDenom > 0.0) (tp + tn) / accuracyDenom else 0.0

    val fpDenom = fp + tn
    val fpr = if(fpDenom > 0.0) fp / fpDenom else 0.0

    val betaSqr = Math.pow(beta, 2.0)

    val fScoreDenom = (betaSqr*precision) + recall
    val fscore = if(fScoreDenom > 0.0){
      (1 + betaSqr) * ((precision*recall) / fScoreDenom)
    } else { 1.0 }

    Scores(mcc, fscore, precision, recall, accuracy, fpr)
  }
}

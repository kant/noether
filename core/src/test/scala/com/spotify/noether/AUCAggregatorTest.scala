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

import org.scalactic.TolerantNumerics
import org.scalatest._

class AUCAggregatorTest extends FlatSpec with Matchers {
  private implicit val doubleEq = TolerantNumerics.tolerantDoubleEquality(0.1)

  private val data =
    List(
      (0.1, 0.0), (0.1, 1.0), (0.4, 0.0), (0.6, 0.0), (0.6, 1.0), (0.6, 1.0), (0.8, 1.0)
    ).map{case(s, pred) => Prediction(pred.toInt, s)}

  it should "return roc auc" in {
    assert(AUCAggregator(ROC, samples=50)(data) === 0.7)
  }

  it should "return pr auc" in {
    assert(AUCAggregator(PR, samples=50)(data) === 0.83)
  }
}
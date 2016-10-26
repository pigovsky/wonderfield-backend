package com.pigovsky.wonderfield.backend.util

import scala.util.Random

/**
  * Created by yp on 26.10.16.
  */
object CollectionUtils {
  def randomItem[T](all: Array[T]) = all(Random.nextInt(all.length))
}

package com.pigovsky.wonderfield.backend.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Created by yp on 26.10.16.
 */
@IgnoreExtraProperties
class GameTask {
  var secret = ""
  var currentWordState: Array[Char] = null
  var message = ""
  def getCurrentWordState = currentWordState.mkString
  def getHint = "Guess a human name"

  def getMessage = message
  def tellSecret = secret
  def update(): Unit = {
    currentWordState = (1 to secret.length map (i => '.')).toArray
  }
}

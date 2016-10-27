package com.pigovsky.wonderfield.backend.model

/**
  * Created by yp on 27.10.16.
  */
class Guess {
  var uid = ""
  var word = ""
  var displayName = ""

  def getUid = uid
  def setUid(uid: String) = this.uid = uid

  def getWord = word
  def setWord(word: String) = this.word = word

  def getDisplayName = displayName
  def setDisplayName(displayName: String) = this.displayName = displayName
}

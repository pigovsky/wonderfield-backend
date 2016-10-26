package com.pigovsky.wonderfield.backend

import java.util.{Timer, TimerTask}

import com.google.firebase.database._
import com.google.firebase.tasks.{OnFailureListener, OnSuccessListener}
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.pigovsky.wonderfield.backend.model.GameTask
import com.pigovsky.wonderfield.backend.util.CollectionUtils

import scala.io.Source

/**
  * Created by yp on 26.10.16.
  */
object WonderFieldBackend extends App {
  val timer = new Timer()
  val options = new FirebaseOptions.Builder()
    .setServiceAccount(getClass.getResourceAsStream("/WonderField-f53a759d49c8.json"))
    .setDatabaseUrl("https://wonderfield-696b4.firebaseio.com/")
    .build()

  FirebaseApp.initializeApp(options)

  val dbRef = FirebaseDatabase.getInstance().getReference()

  val tasks = Source.fromInputStream(getClass.getResourceAsStream("/tasks.txt")).getLines().toArray

  private val gameTask = new GameTask()
  startNewGame()

  dbRef.child("guesses").addChildEventListener(new ChildEventListener {
    override def onChildRemoved(dataSnapshot: DataSnapshot): Unit = {
      println("removed")
    }

    override def onChildMoved(dataSnapshot: DataSnapshot, s: String): Unit = println("moved")

    override def onChildChanged(dataSnapshot: DataSnapshot, s: String): Unit = println("changed")

    override def onCancelled(databaseError: DatabaseError): Unit = println("cancelled")

    override def onChildAdded(dataSnapshot: DataSnapshot, s: String): Unit = {
      val value = dataSnapshot.getValue().toString
      println(s"child added $value $s")
      guess(value)
      updateGameState()
      dataSnapshot.getRef.removeValue()
    }

  })

  while (true) {
    println("*")
    Thread.sleep(5000)
  }

  def guess(word: String): Unit = {
    val secret = gameTask.tellSecret
    if (word.length > 1) {
      if (secret == word) {
        gameTask.message = s"You hit! The word is $word"
        word.copyToArray(gameTask.currentWordState)
        timer.schedule(new TimerTask {
          override def run(): Unit = startNewGame()
        }, 5000)
      } else {
        gameTask.message = s"You loose. It is not $word..."
      }
    } else if (word.nonEmpty) {
      if (secret.toLowerCase().contains(word.toLowerCase())) {
        gameTask.message = s"The secret word does contain $word"
        secret.indices filter (i => secret.toLowerCase()(i) == word.toLowerCase()(0)) foreach
          (i => gameTask.currentWordState(i) = secret(i))
      } else {
        gameTask.message = s"There is no $word letter in the secret word"
      }
    }
  }

  def startNewGame(): Unit = {
    gameTask.secret = CollectionUtils.randomItem(tasks)
    gameTask.update()
    updateGameState()
  }

  def updateGameState(): Unit = {
    val child: DatabaseReference = dbRef.child("gameTask")
    println(s"key child ${child.getKey}")
    child.setValue(gameTask)
      .addOnSuccessListener(new OnSuccessListener[Void] {
        override def onSuccess(tResult: Void): Unit = {
          println(s"ok, secret is " + gameTask.tellSecret)
        }
      })
      .addOnFailureListener(new OnFailureListener {
        override def onFailure(e: Exception): Unit = println(s"error $e")
      })
  }
}

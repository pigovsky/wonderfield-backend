package com.pigovsky.wonderfield.backend

import java.util.concurrent.TimeUnit
import java.util.{Timer, TimerTask}

import com.google.firebase.database._
import com.google.firebase.tasks.{OnFailureListener, OnSuccessListener}
import com.google.firebase.{FirebaseApp, FirebaseOptions}
import com.pigovsky.wonderfield.backend.model.{GameTask, Guess, UserCarma}
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
      val guess = dataSnapshot.getValue(classOf[Guess])
      println(s"child added $guess $s")
      WonderFieldBackend.this.guess(guess)
      dataSnapshot.getRef.removeValue()
    }

  })

  while (true) {
    println("*")
    Thread.sleep(TimeUnit.HOURS.toMillis(12))
  }

  def guess(guess: Guess): Unit = {

    val user = dbRef.child("users").child(guess.uid)

    user.addListenerForSingleValueEvent(new ValueEventListener {
      override def onDataChange(dataSnapshot: DataSnapshot): Unit = {
        var userCarma = dataSnapshot.getValue(classOf[UserCarma])
        if (userCarma == null) {
          userCarma = new UserCarma
        }
        println("user " + userCarma)
        val wordLower = guess.word.toLowerCase()
        val secret = gameTask.tellSecret
        val secretLower = secret.toLowerCase()
        if (wordLower.length > 1) {
          if (secretLower == wordLower) {
            gameTask.message = s"${guess.displayName} has hit the word $secret"
            userCarma.carma += 100
            secret.copyToArray(gameTask.currentWordState)
            timer.schedule(new TimerTask {
              override def run(): Unit = startNewGame()
            }, 5000)
          } else {
            gameTask.message = s"${guess.displayName} has loose, it is not ${guess.word}..."
            userCarma.carma -= 100
          }
        } else if (wordLower.nonEmpty) {
          if (secretLower.contains(wordLower)) {
            gameTask.message = s"${guess.displayName} has hit the letter $wordLower"
            secretLower.indices filter (i => secretLower(i) == wordLower.toLowerCase()(0)) foreach
              (i => gameTask.currentWordState(i) = secret(i))
            userCarma.carma += 1
          } else {
            gameTask.message = s"${guess.displayName}, there is no $wordLower letter in the secret word"
            userCarma.carma -= 1
          }
        }
        user.setValue(userCarma)
        updateGameState()
      }

      override def onCancelled(databaseError: DatabaseError): Unit = {

      }
    })
  }

  def startNewGame(): Unit = {
    gameTask.secret = CollectionUtils.randomItem(tasks)
    gameTask.message = s"We has started a new game. Guess a name of ${gameTask.secret.length} letters"
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

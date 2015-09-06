package com.oomatomo.scaladeamon

object Main extends App {
  println("start")
  new ApplicationDaemon("sample").start
}

import akka.actor.ActorSystem
import com.oomatomo.scaladeamon.SampleActor.CountDown
import scala.concurrent.duration._
import scala.concurrent.Future
import akka.pattern.ask
import akka.pattern.gracefulStop
import scala.concurrent.Await
import akka.actor.{Actor,ActorRef,Props,Terminated,PoisonPill}

class ApplicationDaemon(appName: String) {

  private[this] val actorSystem = ActorSystem(appName)

  def destroy: Unit = {
    println("destroy")
  }

  def start {

    val sampleActor = actorSystem.actorOf(SampleActor.props, "sample")
    var keepRunning = true
    val mainThread = Thread.currentThread()
    Runtime.getRuntime.addShutdownHook(new Thread() {override def run = {
      println("inside addShutDownHook handler")
      keepRunning = false
      mainThread.join()
    }})
    var count = 0
    while (keepRunning) {
      sampleActor ! s"$count"
      count = count + 1
    }
    sampleActor ! PoisonPill
    // Graceful shutdown code goes here that needs to be on the main thread

//    try {
//      val stopped: Future[Boolean] = gracefulStop(sampleActor, 5 seconds, Manager.Shutdown)
//      Await.result(stopped, 6 seconds)
//      // the actor has been stopped
//    } catch {
//      // the actor wasn't stopped within 5 seconds
//      case e: akka.pattern.AskTimeoutException =>
//    }

    println("Exiting main")
    actorSystem.shutdown
  }

  def stop {
    println("終了")
    if (! actorSystem.isTerminated) {
      actorSystem.shutdown
    }
  }
}

object Manager {
  case object Shutdown
}

class Manager extends Actor {
  import Manager._
  val worker = context.watch(context.actorOf(Props[SampleActor], "worker"))

  def receive = {
    case "job" => worker ! "crunch"
    case Shutdown =>
      println("Shutdown")
      worker ! PoisonPill
      context.become(shuttingDown)
  }

  def shuttingDown: Receive = {
    case "job" => sender() ! "service unavailable, shutting down"
    case Terminated(`worker`) =>
      context stop self
  }
}

import akka.actor.{Actor,ActorRef,Props}

class SampleActor extends Actor {

  def receive = {
    case CountDown(0) =>
    context.stop(self)
    case CountDown(n) =>
    println(n)
    Thread.sleep(1000)
    self ! CountDown(n-1)
    case message: String =>
      println(s"00 $message")
      Thread.sleep(3000)
      println(s"30  $message")
  }
}

object SampleActor {
  def props = Props[SampleActor]
  case class CountDown(n: Int) { require(n >= 0) }
}


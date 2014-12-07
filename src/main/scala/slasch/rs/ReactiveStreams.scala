package slasch.rs

import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import akka.stream.FlowMaterializer
import akka.stream.scaladsl.{Sink, Source}
import org.reactivestreams.{Publisher, Subscriber, Subscription}

/**
  * @author  slasch
  * @since   18.11.2014.
  */
object ReactiveStreams {
  class ByteSub(pub: BytePublisher) extends Subscription {
    override def request(l: Long): Unit = {
      pub.request(l)
    }

    override def cancel() {}
  }


  trait BytePublisher extends Publisher[Byte] {
    var subscriber: Subscriber[_ >: Byte] = _

    val counter = new AtomicInteger(0)
    def request(l: Long): Unit = {
      counter.addAndGet(l.toInt)
    }

    var subscription: Subscription = _

    override def subscribe(subscriber: Subscriber[_ >: Byte]) {
      this.subscription = new ByteSub(this)
      this.subscriber = subscriber
      subscriber.onSubscribe(this.subscription)
    }
  }

  trait ByteSubscriber[T >: Byte] extends Subscriber[T] {

    var subscription: Subscription = _

    override def onSubscribe(subscription: Subscription) {
      this.subscription = subscription
    }

    override def onError(t: Throwable) = { }
    override def onComplete() { }
  }

  def create(implicit system: ActorSystem) = {

    val alice = new SThread("RS-Alice") with BytePublisher {
      override def swap(n: Int) {
        val cnt = math.min(n, counter.get())
        counter.addAndGet(-cnt)
        for { i <- 1 to cnt } subscriber.onNext(Env.SCRUPT)
        super.swap(n)
      }
    }

    val borice = new SThread("RS-Borice")
      with ByteSubscriber[Byte] {

      alice.subscribe(this)

      override def swap(n: Int = 1): Unit = {
        subscription.request(n)
      }

      def onNext(t: Byte): Unit = {
        super.swap(1)
      }

    }


/*

    import rx.RxReactiveStreams._
    subscribe(toObservable(alice), borice)

*/
/*
    // Reactor Stream
    Streams.create(alice).subscribe(borice)
*/

    // akka streams
    implicit val mat = FlowMaterializer()(system)
    Source(alice).runWith(Sink(borice))

/*
    //vert.x 3.0 - only supports Streams[Buffer]
    val rws = ReactiveWriteStream.writeStream()
    rws.subscribe(borice)

    val rrs = ReactiveReadStream.readStream()
    alice.subscribe(rrs)

    val pump = Pump.pump(rrs, rws)
    pump.start()
*/

    //Ratpack - only supports Streams[ByteBuf]

    // ratpack.stream.Streams.buffer(alice).subscribe(borice)

    akka.japi.Pair(alice, borice)
  }
}
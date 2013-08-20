/*
 * Copyright (c) 2013 Geoff Ballinger. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.co.geoffballinger.simplesharder

import com.twitter.finagle.service.SimpleRetryPolicy
import com.twitter.finagle.builder.{ClientBuilder,ServerBuilder}
import com.twitter.finagle.http.Http
import com.twitter.finagle.Service
import com.twitter.util.{Future,Try,Duration}
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers
import java.net.InetSocketAddress
import java.nio.charset.Charset

object Sharder extends App {
  try {
    // Collect port arg
    val port = Integer.parseInt(args(0))

    // Build the retry policy
    object policy extends SimpleRetryPolicy[Try[Nothing]]() {
      def backoffAt(retry: Int) = {
        // If we haven't reached the limit retry immediately
        if (retry >= 3) Duration.Top
        else Duration.Zero
      }
      def shouldRetry(arg: Try[Nothing]) = {
        // This demo is idempotent so any request can be retried
        true
      }
    }
    
    // Build up shards from host specs on remaining command line
    val shards = args.tail.map(spec => {
      ClientBuilder()
      .codec(Http())
      .hosts(spec)
      .hostConnectionLimit(10)
      .retryPolicy(policy)
      .build()
    })
    
    val CHARSET = Charset.forName("UTF-8")
    
    // Define our service - scatter to the shards and gather the results
    val service = new Service[HttpRequest, HttpResponse] {
	  def apply(req: HttpRequest) = {
	      Future.collect(shards.map(shard => {                       // Scatter
            shard(req).map(resp => resp.getContent().toString(CHARSET))
          }))
          .map(resps => {                                            // Gather
            val buffer = ChannelBuffers.copiedBuffer(resps.reduceLeft(_+":"+_), CHARSET)
		    val response = new DefaultHttpResponse(req.getProtocolVersion, HttpResponseStatus.OK)
		    response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain")
		    response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes())
		    response.setContent(buffer)
            response
        })
	  }
    }
    
    // Run this shard over the required port
	val server = ServerBuilder()
	.codec(Http())
	.bindTo(new InetSocketAddress(port))
	.name("Sharder")
	.build(service)
	
  } catch {
    case t: Throwable => {
      System.err.println(t.getMessage())
      System.err.println("Usage: Sharder port <shard>*")
    }
  }
}
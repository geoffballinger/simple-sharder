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

import com.twitter.finagle.builder.ServerBuilder
import com.twitter.finagle.http.Http
import com.twitter.finagle.Service
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http._
import org.jboss.netty.buffer.ChannelBuffers
import java.net.InetSocketAddress
import java.nio.charset.Charset

object Worker extends App {
  
  try {
    // Collect args
    val port = Integer.parseInt(args(0))
    val value = args(1)
    
    // Define our service - just return the value!
    val service = new Service[HttpRequest, HttpResponse] {
	  def apply(req: HttpRequest) = {
	    val buffer = ChannelBuffers.copiedBuffer(value, Charset.forName("UTF-8"))
	    val response = new DefaultHttpResponse(req.getProtocolVersion, HttpResponseStatus.OK)
	    response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/plain")
	    response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, buffer.readableBytes())
        response.setContent(buffer)
	    Future.value(response)
	  }
    }
    
    // Run this worker over the required port
	val server = ServerBuilder()
	.codec(Http())
	.bindTo(new InetSocketAddress(port))
	.name("Worker")
	.build(service)
	
  } catch {
    case t: Throwable => {
      System.err.println(t.getMessage())
      System.err.println("Usage: Worker <port> <value>")
    }
  }
}
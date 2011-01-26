/*
* JBoss, Home of Professional Open Source.
* Copyright 2006, Red Hat Middleware LLC, and individual contributors
* as indicated by the @author tags. See the copyright.txt file in the
* distribution for a full listing of individual contributors.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/
package org.jboss.as.controller.test;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.ADDRESS;

import java.net.InetAddress;
import java.util.concurrent.CancellationException;

import org.jboss.as.controller.Cancellable;
import org.jboss.as.controller.ModelController;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.ProxyController;
import org.jboss.as.controller.ResultHandler;
import org.jboss.as.controller.remote.ModelControllerClientToModelControllerAdapter;
import org.jboss.dmr.ModelNode;
import org.junit.After;
import org.junit.Before;

/**
 *
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @version $Revision: 1.1 $
 */
public class RemoteProxyControllerTestCase extends AbstractProxyControllerTest {

    private static final int PORT = 12345;
    RemoteModelControllerSetup server;
    ModelController proxyController;
    ModelController remoteModelController;

    @Before
    public void start() throws Exception {
        server = new RemoteModelControllerSetup(proxyController, PORT);
        server.start();
        remoteModelController = new ModelControllerClientToModelControllerAdapter(InetAddress.getByName("localhost"), PORT);
    }

    @After
    public void stop() {
        server.stop();
    }

    @Override
    protected ProxyController createProxyController(final ModelController targetController, final PathAddress proxyNodeAddress) {
        this.proxyController = targetController;
        return new RemoteProxyController(proxyNodeAddress);
    }

    private class RemoteProxyController implements ProxyController {
        private final PathAddress proxyNodeAddress;

        public RemoteProxyController(final PathAddress proxyNodeAddress) {
            super();
            this.proxyNodeAddress = proxyNodeAddress;
        }

        @Override
        public PathAddress getProxyNodeAddress() {
            return proxyNodeAddress;
        }

        @Override
        public Cancellable execute(final ModelNode operation, final ResultHandler resultHandler) {
            final ModelNode newOperation = operation.clone();
            final ModelNode address = newOperation.require(ADDRESS);
            PathAddress path = PathAddress.pathAddress(address);
            newOperation.get(ADDRESS).set(path.subAddress(proxyNodeAddress.size()).toModelNode());
            return remoteModelController.execute(newOperation, resultHandler);
        }

        @Override
        public ModelNode execute(final ModelNode operation) throws CancellationException, OperationFailedException {
            return remoteModelController.execute(operation);
        }
    }
}

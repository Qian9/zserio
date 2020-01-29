import grpc
from concurrent import futures

import service_poc.api as api
import zserio_service_grpc.zserio_service

class SimpleServiceImpl(api.SimpleService.Service):
    """ Implementation of zserio SimpleService """

    def _powerOfTwoImpl(self, request):
        return api.Response.fromFields(request.getValue() ** 2)

    def _powerOfFourImpl(self, request):
        return api.Response.fromFields(request.getValue() ** 4)

def doCall(service, value):
    # SimpleService client generated by zserio
    client = api.SimpleService.Client(service)
    request = api.Request.fromFields(value)
    response = client.callPowerOfTwo(request)
    print("    powerOfTwo(%d) = %d" % (value, response.getValue()))
    response = client.callPowerOfFour(request)
    print("    powerOfFour(%d) = %d" % (value, response.getValue()))

def directCall(service, value):
    print("calling service directly:")
    doCall(service, value)
    
def grpcCall(service, value):
    print("calling service via gRPC:")

    # setup grpc server
    grpcServer = grpc.server(futures.ThreadPoolExecutor())
    port = grpcServer.add_insecure_port("localhost:0")
    # wraps SimpleService by GrpcServicer and registers it to the server
    zserio_service_grpc.zserio_service.registerService(service, grpcServer)
    grpcServer.start()

    # setup grpc client
    grpcChannel = grpc.insecure_channel("localhost:%d" % port)
    grpcClient = zserio_service_grpc.zserio_service.GrpcClient(grpcChannel)

    # use zserio GrpcClient instead of zserio service
    doCall(grpcClient, value)

if __name__ == "__main__":
    # create SimpleService implementation
    service = SimpleServiceImpl()

    directCall(service, 2)
    grpcCall(service, 3)

# what is controller ?
    This is the layer that:
        Receives HTTP request
        Calls service
        Returns response


# Client → Controller → Service → Repository → DB

Controller should:
    Accept request
    Validate input
    Return response

Controller should NOT:
    Contain business logic
    Write database logic
    That belongs to Service.



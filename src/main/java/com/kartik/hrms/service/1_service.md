# Why Do We Need Service Layer ?
    Why not call repository directly from controller? Because in real applications:
        Controller should:
            Handle request
            Validate input
            Return response

        Service should:
            Apply business rules
            Handle logic
            Call repository
            Manage transactions

# 📐 Clean Architecture Flow
    Controller  →  Service  →  Repository  →  Database

If you skip Service layer:
    Code becomes messy
    Hard to scale
    Hard to maintain
    Hard to test
    We build properly.

# @Service
    Marks this class as:
    Business logic layer component
    Spring will auto-detect and register it.

# constructor injection
    public UserService(UserRepository userRepository)

    Spring automatically injects UserRepository.
        This is called:
        Dependency Injection


# in Employee why we inject both repository 
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    Because:
        Employee depends on User
        We must verify user exists before creating employee
            This is called:
                Service Composition


# What Is a Repository?
    In simple words:    Repository = Class that talks to database

Instead of writing SQL manually like:
    SELECT * FROM users WHERE username = ?

Spring lets us write:
    userRepository.findByUsername("kartik");

# it generates SQL automatically.

extends JpaRepository<User, Long>
    It means:
    This repository works with User entity     Primary key type is Long    By extending JpaRepository, you automatically get:
    
    save()
    findById()
    findAll()
    delete()
    count()
    existsById()

Without writing any SQL.

# What is Optional<User>
    Instead of returning User, we return:  Optional<User>
# why 
    Because user may or may not exist., This prevents NullPointerException.

# How Does This Work Without SQL?
    Spring reads method name:  <findByUsername> and converts it into SELECT * FROM users WHERE username = ?

# This is called:
    Query Method Derivation
        Spring generates query from method name.


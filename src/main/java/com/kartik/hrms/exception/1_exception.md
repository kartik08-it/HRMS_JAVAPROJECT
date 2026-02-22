# What Is an Exception?
An Exception represents an abnormal situation in your application.
Example:
    User not found
    Username already exists
    Unauthorized access
    Invalid request data
    Instead of crashing the application, we throw an exception and return a proper HTTP response.

# 🏗 Why Custom Exceptions?
If we throw:
    throw new RuntimeException("User not found");

Spring returns:
{
  "status": 500,
  "error": "Internal Server Error"
}

Problems:
    Wrong HTTP status
    Not descriptive
    Not professional
    Hard to control response format

# Exception Class  →  GlobalExceptionHandler  →  JSON Response


# Why Only One Global Handler?

Because:
    Central control
    Consistent response structure
    Easy to maintain
    Scalable
    You do NOT create multiple handler files.

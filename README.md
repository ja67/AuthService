### Third party libraries used in the project:
```xml
<dependencies>
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20220320</version>
        <!-- For JSON format request&response handling-->
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <version>1.18.24</version>
        <!-- For more convenient data object usage-->
    </dependency>
    <dependency>
        <groupId>org.redisson</groupId>
        <artifactId>redisson</artifactId>
        <version>3.17.4</version>
        <!-- For redis opreations-->
    </dependency>
</dependencies>
```
### Create User
    POST localhost:8001/user
    {
	    "userName":"test"
	    "password":"pass"
    }
### Delete User
    DELETE localhost:8001/user?userName=test
### Create Role
    POST localhost:8001/role
    {
	    "roleName":"test"
    }
### Delete Role
    DELETE localhost:8001/role?userName=test
### Add role to user
    POST localhost:8001/grant
    {
	    "userName":"test",
	    "roleName":"test"
    }
### Authenticate
    POST localhost:8001/auth
    {
	    "userName":"test",
	    "pass":"test"
    }
### Invalidate
    DELETE localhost:8001/auth?token=392e89e4000bf4ff4faa94128f91a110e840aa5fb329acc6f4f5bc25e9f9c3df
### Check role
    GET localhost:8001/role?roleName=test&token=392e89e4000bf4ff4faa94128f91a110e840aa5fb329acc6f4f5bc25e9f9c3df
### All roles
    GET localhost:8001/role?token=392e89e4000bf4ff4faa94128f91a110e840aa5fb329acc6f4f5bc25e9f9c3df

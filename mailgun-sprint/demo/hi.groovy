@Grab("org.springframework.boot:spring-boot-starter-actuator:1.5.4.RELEASE")
@RestController
class HiRestController {

    @GetMapping("/hi")
    def hi() {
        "Hi"
    }

}

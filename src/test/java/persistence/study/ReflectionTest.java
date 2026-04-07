package persistence.study;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class ReflectionTest {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionTest.class);

    @Test
    @DisplayName("Car 객체 정보 가져오기")
    void showClass() throws NoSuchFieldException, NoSuchMethodException {
        Class<Car> carClass = Car.class;

        //모든 필드 목록과 각 필드의 타입
Arrays.stream(carClass.getDeclaredFields())
        .forEach(field -> logger.debug("Field: {} ({})", field.getName(), field.getType()));

//모든 생성자 목록과 각 생성자의 파라미터
Arrays.stream(carClass.getDeclaredConstructors())
        .forEach(constructor -> logger.debug("Constructor: {} ({})", constructor.getName(), Arrays.toString(constructor.getParameterTypes())));

//모든 메서드 목록과 각 메서드의 반환 타입
Arrays.stream(carClass.getDeclaredMethods())
        .forEach(method -> logger.debug("Method: {} -> {}", method.getName(), method.getReturnType()));

    }

    @Test
    @DisplayName("test로 시작하는 메서드 실행")
    void testMethodRun(){
        Class<Car> carClass = Car.class;

        // Method: testGetName() -> Result: test : null
        // Method: testGetPrice() -> Result: test : 0

            Arrays.stream(carClass.getMethods())
                    .filter(method -> method.getName().startsWith("test"))
                    .forEach(method -> {
                        try {
                            logger.debug("Method: {}() -> Result: {}", method.getName(), method.invoke(carClass.getConstructor().newInstance()));
                        } catch (Exception e) {
                            logger.error("Error invoking method: {}", method.getName(), e);
                        }
                    });

    }

    @Test
    @DisplayName("@PrintView 애노테이션 메서드 실행")
    void testAnnotationMethodRun() throws Exception {
        Class<Car> carClass = Car.class;

            Arrays.stream(carClass.getMethods())
                    .filter(method -> method.isAnnotationPresent(PrintView.class))
                    .forEach(method -> {
                        try {
                            logger.debug("Invoking method: {}", method.getName());
                            method.invoke(carClass.getConstructor().newInstance());
                        } catch (Exception e) {
                            logger.error("Error invoking method: {}", method.getName(), e);
                        }
                    });
    }


    @Test
    @DisplayName("private 필드에 값 할당")
    void privateFieldAccess() throws Exception {
        Class<Car> carClass = Car.class;

        var nameField = carClass.getDeclaredField("name");
        var priceField = carClass.getDeclaredField("price");

        nameField.setAccessible(true);
        priceField.setAccessible(true);

        var carInstance = new Car();

        logger.debug("Car instance created with name: {} and price: {}", carInstance.getName(), carInstance.getPrice());

        nameField.set(carInstance, "Sonata");
        priceField.set(carInstance, 30000);

        logger.debug("Car instance created with name: {} and price: {}", carInstance.getName(), carInstance.getPrice());

        assertThat(carInstance.getName()).isEqualTo("Sonata");
        assertThat(carInstance.getPrice()).isEqualTo(30000);
    }

    @Test
    @DisplayName("인자를 가진 생성자의 인스턴스 생성")
    void constructorWithArgs() throws Exception {
        Class<Car> carClass = Car.class;

        var constructor = carClass.getConstructor(String.class, int.class);
        var carInstance = constructor.newInstance("Sonata", 30000);

        logger.debug("Car instance created with name: {} and price: {}", carInstance.getName(), carInstance.getPrice());
        assertThat(carInstance.getName()).isEqualTo("Sonata");
        assertThat(carInstance.getPrice()).isEqualTo(30000);
    }


}

package study;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ReflectionTest {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionTest.class);

    @Test
    @DisplayName("Car 객체 정보 가져오기")
    void showClass() {
        Class<Car> carClass = Car.class;

        Arrays.stream(carClass.getDeclaredFields())
                .forEach(field -> logger.debug("Field: {} ({})", field.getName(), field.getType()));

        Arrays.stream(carClass.getDeclaredConstructors())
                .forEach(constructor -> logger.debug("Constructor: {} ({})",
                        constructor.getName(),
                        formatParameters(constructor.getParameterTypes())));

        Arrays.stream(carClass.getDeclaredMethods())
                .forEach(method -> logger.debug("Method: {}({}) -> {}",
                        method.getName(),
                        formatParameters(method.getParameterTypes()),
                        method.getReturnType()));
    }

    @Test
    @DisplayName("test로 시작하는 메서드 실행")
    void testMethodRun() throws Exception {
        Class<Car> carClass = Car.class;
        Car car = new Car();

        for (Method method : carClass.getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                logger.debug("Method: {}({}) -> Result: {}",
                        method.getName(),
                        formatParameters(method.getParameterTypes()),
                        method.invoke(car));
            }
        }
    }

    @Test
    @DisplayName("@PrintView 애노테이션 메서드 실행")
    void testAnnotationMethodRun() throws Exception {
        Class<Car> carClass = Car.class;
        Car car = new Car();

        for (Method method : carClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PrintView.class)) {
                method.invoke(car);
            }
        }
    }

    @Test
    @DisplayName("private 필드에 값 할당")
    void privateFieldAccess() throws Exception {
        Class<Car> carClass = Car.class;
        Car car = new Car();

        Field nameField = carClass.getDeclaredField("name");
        nameField.setAccessible(true);
        nameField.set(car, "투싼");

        Field priceField = carClass.getDeclaredField("price");
        priceField.setAccessible(true);
        priceField.set(car, 3000);

        logger.debug("Car name: {}", car.getName());
        logger.debug("Car price: {}", car.getPrice());
    }

    @Test
    @DisplayName("인자를 가진 생성자의 인스턴스 생성")
    void constructorWithArgs() throws Exception {
        Class<Car> carClass = Car.class;

        Constructor<Car> constructor = carClass.getDeclaredConstructor(String.class, int.class);
        Car car = constructor.newInstance("투싼", 4000);

        logger.debug("Car name: {}", car.getName());
        logger.debug("Car price: {}", car.getPrice());
    }

    private String formatParameters(Class<?>[] parameterTypes) {
        return Arrays.stream(parameterTypes)
                .map(Class::getTypeName)
                .collect(Collectors.joining(", "));
    }
}
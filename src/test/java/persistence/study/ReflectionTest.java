package persistence.study;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ReflectionTest {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionTest.class);

    @Test
    @DisplayName("Car 객체 정보 가져오기")
    void showClass() {
        Class<Car> carClass = Car.class;
        logger.debug(carClass.getName());

        // 여기에 코드 작성
        fieldName(carClass);
        constructorName(carClass);
        methodName(carClass);
    }

    @Test
    @DisplayName("test로 시작하는 메서드 실행")
    void testMethodRun() throws Exception {
        Class<Car> carClass = Car.class;

        // 여기에 코드 작성
        invokeAll(carClass);
    }

    @Test
    @DisplayName("@PrintView 애노테이션 메서드 실행")
    void testAnnotationMethodRun() throws Exception {
        Class<Car> carClass = Car.class;

        // 여기에 코드 작성
        invokeAnnotationMethod(carClass);
    }

    @Test
    @DisplayName("private 필드에 값 할당")
    void privateFieldAccess() throws Exception {
        Class<Car> carClass = Car.class;

        // 여기에 코드 작성
        Map<String, Object> fieldCommand = makePrivateFieldCommand(carClass,
                Map.of(
                        "name", "test",
                        "price", 10000
                ));

        Car car = makeCar(carClass, fieldCommand);

        assertThat(car.getName()).isEqualTo("test");
        assertThat(car.getPrice()).isEqualTo(10000);
    }

    @Test
    @DisplayName("인자를 가진 생성자의 인스턴스 생성")
    void constructorWithArgs() throws Exception {
        Class<Car> carClass = Car.class;

        // 여기에 코드 작성
        Map<String, Object> fieldCommand = makePrivateFieldCommand(carClass,
                Map.of(
                        "name", "test",
                        "price", 10000
                ));

        Car car = makeCar(carClass, fieldCommand);

        assertThat(car.getName()).isEqualTo("test");
        assertThat(car.getPrice()).isEqualTo(10000);
    }



    private void fieldName(Class<Car> carClass) {
        List<String> typeName = Arrays.stream(carClass.getDeclaredFields())
                .map(it -> parameterName(toArray(it.getType())))
                .toList();

        List<String> fieldName = Arrays.stream(carClass.getDeclaredFields())
                .map(Field::getName)
                .toList();

        for (int i = 0; i < typeName.size(); i++) {
            System.out.println("Field: " + fieldName.get(i) + " (" + typeName.get(i) + ")");
        }
    }

    private String parameterName(Class<?>[] parameterTypes) {
        return Arrays.stream(parameterTypes)
                .map(Class::getSimpleName)
                .collect(Collectors.joining(", "));
    }

    private void constructorName(Class<Car> carClass) {
        List<String> parameterNames = Arrays.stream(carClass.getDeclaredConstructors())
                .map(it -> "(" + parameterName(it.getParameterTypes()) + ")")
                .toList();

        for (String parameterName : parameterNames) {
            System.out.println("Constructor: " + carClass.getSimpleName() + parameterName);
        }
    }

    private Class<?>[] toArray(Class<?> parameterType) {
        return new Class[]{parameterType};
    }

    private void methodName(Class<Car> carClass) {
        List<String> parameterNames = Arrays.stream(carClass.getDeclaredMethods())
                        .map(it -> parameterName(toArray(it.getReturnType())))
                        .toList();

        List<String> methodName = Arrays.stream(carClass.getDeclaredMethods())
                        .map(Method::getName)
                        .toList();

        for (int i = 0; i < methodName.size(); i++) {
            System.out.println("Method: " + methodName.get(i)+ "()" + " -> " + parameterNames.get(i));
        }
    }

    private void invokeAll(Class<Car> carClass) {
        Arrays.stream(carClass.getDeclaredMethods())
                .filter(it -> it.getName().contains("test"))
                .forEach(
                        it -> {
                            try {
                                Object result = it.invoke(new Car());
                                System.out.println("Method: " + it.getName() + "() -> Result: " +  result);
                            } catch (Exception ignore) {}
                        }
                );
    }

    private void invokeAnnotationMethod(Class<Car> carClass) {
        Arrays.stream(carClass.getDeclaredMethods())
                .filter(it -> it.getAnnotation(PrintView.class) != null)
                .forEach(it -> {
                    try {
                        Object res = it.invoke(new Car());
                        System.out.println("Method: " + it.getName() + " -> Result: " + res);
                    } catch (Exception ignored) {
                    }
                });
    }

    private List<String> findPrivateFieldName(Class<Car> carClass) {
        return Arrays.stream(carClass.getDeclaredFields())
                .filter(it -> it.getModifiers() == Modifier.PRIVATE)
                .map(Field::getName)
                .toList();
    }

    private Map<String, Object> makePrivateFieldCommand(Class<Car> carClass, Map<String, Object> commandMap) {
        List<String> keys = findPrivateFieldName(carClass);
        Map<String, Object> result = new HashMap<>();
        for (String key : keys) {
            Object value = commandMap.get(key);
            result.put(key, value);
        }
        return result;
    }

    private Car makeCar(Class<Car> carClass, Map<String, Object> fieldCommand) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Car car = carClass.getDeclaredConstructor().newInstance();

        Arrays.stream(carClass.getDeclaredFields())
                .filter(field -> fieldCommand.containsKey(field.getName()))
                .forEach(field -> {
                    field.setAccessible(true);
                    Object value = fieldCommand.get(field.getName());
                    try {
                        field.set(car, value);
                    } catch (Exception ignore) {}
                });

        return car;
    }
}

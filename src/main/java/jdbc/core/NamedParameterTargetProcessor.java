package jdbc.core;

import jdbc.vo.NamedParameterBindCommand;

import java.util.List;

public interface NamedParameterTargetProcessor extends QueryManager {

    void processParameter(String name, Object value);

    List<NamedParameterBindCommand> getCommand();
}

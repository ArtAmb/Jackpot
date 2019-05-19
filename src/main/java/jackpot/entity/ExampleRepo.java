package jackpot.entity;

import jackpot.orm.repository.JackpotRepository;

public interface ExampleRepo extends JackpotRepository<Integer, ExampleTable> {

    ExampleTable[] findByIntCol(Integer intCol);

}

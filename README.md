# This repo contains code and docs regarding Evolutionary Algorithms project classes

## Project topic
Skew-symmetric version of Self Avoiding Walk  

## Milestone 1 
  1. Choose technology, programming language, environment  
    - we have decided, we're going to go with [Scala](https://www.scala-lang.org)  
  2. Implement LABS and evaluation function  
    - Labs and evaluation function can be found [here](src/main/scala/main.scala#L4)  
  3. Try to understand your algorithm - implement it or write in pseudo code  
    - following the [PDF](docs/LABS+emas+memetic_PL.pdf), the code can be written [as follows]()
  4. Prepare basic implementation which provides:  
    - calculation for specific sequence length,  
    - terminate after defined amount of time,  
    - outputs periodicaly best achieved result,  
    - constructs plot for time-result function

----------

## Usage

### To run, place config file in `src/main/resources`
```
sbt "run -c config_file_in_resources.conf -o output_logfile.log"
```

FROM clojure
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN ["lein", "with-profile", "docker", "install"]
CMD ["lein", "with-profile", "docker", "run"]


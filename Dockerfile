FROM clojure
COPY . /usr/src/app
WORKDIR /usr/src/app
RUN ["lein", "with-profile", "production", "install"]
CMD ["lein", "with-profile", "production", "run"]


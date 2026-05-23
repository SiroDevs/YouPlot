import 'package:dartz/dartz.dart';
import 'package:equatable/equatable.dart';

import '../../core/errors/failures.dart';
import '../../core/usecases/usecase.dart';
import '../entities/location.dart';
import '../repositories/repositories.dart';

class SearchPlaces extends UseCase<List<Location>, SearchPlacesParams> {
  final LocationRepository _repo;
  SearchPlaces(this._repo);

  @override
  Future<Either<Failure, List<Location>>> call(SearchPlacesParams p) =>
      _repo.searchPlaces(p.query);
}

class SearchPlacesParams extends Equatable {
  final String query;
  const SearchPlacesParams(this.query);
  @override
  List<Object?> get props => [query];
}

import 'dart:io';
import 'dart:math';

import 'package:dartz/dartz.dart';
import 'package:path_provider/path_provider.dart';
import 'package:pdf/pdf.dart';
import 'package:pdf/widgets.dart' as pw;

import '../../core/constants/app_constants.dart';
import '../../core/errors/failures.dart';
import '../../core/utils/formatters.dart';
import '../../domain/entities/route_plan.dart';
import '../../domain/repositories/repositories.dart';

class ExportRepositoryImpl implements ExportRepository {
  @override
  Future<Either<Failure, String>> toGpx(RoutePlan plan) async {
    try {
      final r = plan.route;
      final sb = StringBuffer()
        ..writeln('<?xml version="1.0" encoding="UTF-8"?>')
        ..writeln('<gpx version="1.1" creator="$kAppName" xmlns="http://www.topografix.com/GPX/1/1">')
        ..writeln('  <metadata>')
        ..writeln('    <name>${_e(r.origin.name)} to ${_e(r.destination.name)}</name>')
        ..writeln('    <desc>${r.sport.label} · ${r.totalDistanceKm.toStringAsFixed(1)} km · +${r.totalAscentM.toStringAsFixed(0)} m</desc>')
        ..writeln('    <time>${plan.startTime.toUtc().toIso8601String()}</time>')
        ..writeln('  </metadata>');

      for (final wp in r.waypoints) {
        sb
          ..writeln('  <wpt lat="${wp.location.lat}" lon="${wp.location.lng}">')
          ..writeln('    <name>${_e(wp.label)}</name>')
          ..writeln('  </wpt>');
      }

      for (final b in plan.breaks) {
        if (b.distanceFromStartKm != null) {
          final coord = _coordAt(r.geometry, b.distanceFromStartKm!);
          sb
            ..writeln('  <wpt lat="${coord[1]}" lon="${coord[0]}">')
            ..writeln('    <name>${_e(b.type.label)}</name>')
            ..writeln('    <time>${b.scheduledAt.toUtc().toIso8601String()}</time>')
            ..writeln('  </wpt>');
        }
      }

      sb
        ..writeln('  <trk>')
        ..writeln('    <name>${_e(r.origin.name)} to ${_e(r.destination.name)}</name>')
        ..writeln('    <type>${r.sport.label}</type>')
        ..writeln('    <trkseg>');

      for (int i = 0; i < r.geometry.length; i++) {
        final c = r.geometry[i];
        final idx = r.elevation.isEmpty
            ? 0
            : (i / r.geometry.length * r.elevation.length).floor().clamp(0, r.elevation.length - 1);
        final ele = r.elevation.isEmpty ? 0.0 : r.elevation[idx].elevationM;
        sb.writeln('      <trkpt lat="${c[1]}" lon="${c[0]}"><ele>${ele.toStringAsFixed(1)}</ele></trkpt>');
      }

      sb
        ..writeln('    </trkseg>')
        ..writeln('  </trk>')
        ..writeln('</gpx>');

      return Right(await _saveText('route_${plan.id}.gpx', sb.toString()));
    } catch (e) {
      return Left(ExportFailure('GPX export failed: $e'));
    }
  }

  @override
  Future<Either<Failure, String>> toPdf(RoutePlan plan) async {
    try {
      final r = plan.route;
      final u = r.unit;
      final doc = pw.Document();

      doc.addPage(pw.MultiPage(
        pageFormat: PdfPageFormat.a4,
        margin: const pw.EdgeInsets.all(40),
        build: (_) => [
          pw.Container(
            padding: const pw.EdgeInsets.all(20),
            decoration: pw.BoxDecoration(color: PdfColors.teal700, borderRadius: pw.BorderRadius.circular(10)),
            child: pw.Column(crossAxisAlignment: pw.CrossAxisAlignment.start, children: [
              pw.Text(kAppName, style: const pw.TextStyle(color: PdfColors.white, fontSize: 10)),
              pw.SizedBox(height: 4),
              pw.Text('${r.origin.name ?? "Start"} → ${r.destination.name ?? "End"}',
                  style: pw.TextStyle(color: PdfColors.white, fontSize: 20, fontWeight: pw.FontWeight.bold)),
              pw.SizedBox(height: 2),
              pw.Text(r.sport.label, style: const pw.TextStyle(color: PdfColors.white, fontSize: 12)),
            ]),
          ),
          pw.SizedBox(height: 20),
          pw.Row(mainAxisAlignment: pw.MainAxisAlignment.spaceEvenly, children: [
            _stat('Distance', Fmt.distance(r.totalDistanceKm, u)),
            _stat('Ascent', '+${Fmt.elevation(r.totalAscentM, u)}'),
            _stat('Descent', '-${Fmt.elevation(r.totalDescentM, u)}'),
            _stat('Days', '${plan.totalDays}'),
            _stat('Total time', Fmt.duration(plan.estimatedTotal)),
          ]),
          pw.SizedBox(height: 20),
          pw.Text('Daily schedule', style: pw.TextStyle(fontSize: 14, fontWeight: pw.FontWeight.bold)),
          pw.SizedBox(height: 8),
          pw.Table(
            border: pw.TableBorder.all(color: PdfColors.grey300, width: 0.5),
            children: [
              _row(['Day', 'Distance', 'Depart', 'Arrive', 'Breaks'], h: true),
              ...plan.segments.map((s) => _row([
                    'Day ${s.day}',
                    Fmt.distance(s.distanceKm, u),
                    Fmt.hhmm(s.departureTime),
                    Fmt.hhmm(s.estimatedArrival),
                    s.breaks.map((b) => b.type.label).join(', '),
                  ])),
            ],
          ),
          if (plan.breaks.isNotEmpty) ...[
            pw.SizedBox(height: 20),
            pw.Text('Break schedule', style: pw.TextStyle(fontSize: 14, fontWeight: pw.FontWeight.bold)),
            pw.SizedBox(height: 8),
            pw.Table(
              border: pw.TableBorder.all(color: PdfColors.grey300, width: 0.5),
              children: [
                _row(['Break', 'Time', 'Duration'], h: true),
                ...plan.breaks.map((b) => _row([
                      '${b.type.emoji} ${b.type.label}',
                      Fmt.hhmm(b.scheduledAt),
                      Fmt.duration(b.duration),
                    ])),
              ],
            ),
          ],
          pw.SizedBox(height: 20),
          pw.Divider(),
          pw.Text('Generated by $kAppName · ${DateTime.now().toString().substring(0, 10)}',
              style: const pw.TextStyle(fontSize: 8, color: PdfColors.grey)),
        ],
      ));

      return Right(await _savePdf('route_${plan.id}.pdf', doc));
    } catch (e) {
      return Left(ExportFailure('PDF export failed: $e'));
    }
  }

  @override
  Future<Either<Failure, String>> toImage(RoutePlan plan) async {
    try {
      final r = plan.route;
      final doc = pw.Document();
      doc.addPage(pw.Page(
        pageFormat: const PdfPageFormat(800, 480, marginAll: 0),
        build: (_) => pw.Container(
          color: PdfColors.grey900,
          padding: const pw.EdgeInsets.all(40),
          child: pw.Column(mainAxisAlignment: pw.MainAxisAlignment.center, children: [
            pw.Text('${r.origin.name ?? "Start"} → ${r.destination.name ?? "End"}',
                style: pw.TextStyle(color: PdfColors.white, fontSize: 24, fontWeight: pw.FontWeight.bold)),
            pw.SizedBox(height: 20),
            pw.Row(mainAxisAlignment: pw.MainAxisAlignment.spaceEvenly, children: [
              _statLight('Sport', '${r.sport.emoji} ${r.sport.label}'),
              _statLight('Distance', Fmt.distance(r.totalDistanceKm, r.unit)),
              _statLight('Ascent', '+${Fmt.elevation(r.totalAscentM, r.unit)}'),
              _statLight('Days', '${plan.totalDays}'),
            ]),
            pw.SizedBox(height: 16),
            pw.Text(kAppName, style: const pw.TextStyle(color: PdfColors.teal200, fontSize: 11)),
          ]),
        ),
      ));
      return Right(await _savePdf('route_${plan.id}_card.pdf', doc));
    } catch (e) {
      return Left(ExportFailure('Image export failed: $e'));
    }
  }

  // ── Helpers ───────────────────────────────────────────────────────────────

  Future<String> _saveText(String name, String s) async {
    final f = File('${(await getTemporaryDirectory()).path}/$name');
    await f.writeAsString(s, flush: true);
    return f.path;
  }

  Future<String> _savePdf(String name, pw.Document doc) async {
    final f = File('${(await getTemporaryDirectory()).path}/$name');
    await f.writeAsBytes(await doc.save(), flush: true);
    return f.path;
  }

  String _e(String? s) =>
      (s ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('>', '&gt;');

  List<double> _coordAt(List<List<double>> g, double km) {
    double cum = 0;
    for (int i = 1; i < g.length; i++) {
      cum += _hav(g[i - 1][1], g[i - 1][0], g[i][1], g[i][0]);
      if (cum >= km) return g[i];
    }
    return g.isEmpty ? [0, 0] : g.last;
  }

  double _hav(double la1, double lo1, double la2, double lo2) {
    const r = 6371.0;
    final dLa = (la2 - la1) * pi / 180;
    final dLo = (lo2 - lo1) * pi / 180;
    final a = sin(dLa / 2) * sin(dLa / 2) +
        cos(la1 * pi / 180) * cos(la2 * pi / 180) * sin(dLo / 2) * sin(dLo / 2);
    return r * 2 * atan2(sqrt(a), sqrt(1 - a));
  }

  pw.Widget _stat(String l, String v) => pw.Column(children: [
        pw.Text(l, style: const pw.TextStyle(fontSize: 9, color: PdfColors.grey700)),
        pw.SizedBox(height: 4),
        pw.Text(v, style: pw.TextStyle(fontSize: 13, fontWeight: pw.FontWeight.bold)),
      ]);

  pw.Widget _statLight(String l, String v) => pw.Column(children: [
        pw.Text(l, style: const pw.TextStyle(fontSize: 9, color: PdfColors.white)),
        pw.SizedBox(height: 4),
        pw.Text(v, style: pw.TextStyle(fontSize: 13, fontWeight: pw.FontWeight.bold, color: PdfColors.white)),
      ]);

  pw.TableRow _row(List<String> cells, {bool h = false}) => pw.TableRow(
        decoration: h ? const pw.BoxDecoration(color: PdfColors.grey200) : null,
        children: cells
            .map((c) => pw.Padding(
                  padding: const pw.EdgeInsets.all(6),
                  child: pw.Text(c,
                      style: pw.TextStyle(fontSize: 10, fontWeight: h ? pw.FontWeight.bold : pw.FontWeight.normal)),
                ))
            .toList(),
      );
}

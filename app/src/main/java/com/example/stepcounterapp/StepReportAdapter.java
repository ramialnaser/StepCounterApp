package com.example.stepcounterapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// this adapter creates a list of layouts (reports) from the values passed in the reports fragment
public class StepReportAdapter extends RecyclerView.Adapter<StepReportAdapter.StepsViewHolder> {

    private List<DailyReport> reports;


    public StepReportAdapter(List<DailyReport> reports) {
        this.reports = reports;

    }

    @NonNull
    @Override
    public StepReportAdapter.StepsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item,parent,false);
        StepsViewHolder stepsViewHolder = new StepsViewHolder(view);

        return stepsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull StepsViewHolder holder, int position) {
    DailyReport report = reports.get(position);
    holder.dateText.setText(report.getDate());
    holder.totalStepsText.setText(String.valueOf(report.getTotalSteps()));
    holder.distanceText.setText(String.format("%.4f",report.getDistance()));
    holder.caloriesText.setText(String.valueOf(report.getCalories()));

    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public class StepsViewHolder extends RecyclerView.ViewHolder {

        public TextView totalStepsText, distanceText,caloriesText,dateText;

        public StepsViewHolder(@NonNull View itemView) {
            super(itemView);
            dateText = (TextView) itemView.findViewById(R.id.report_date);
            totalStepsText = (TextView) itemView.findViewById(R.id.report_steps);
            distanceText = (TextView) itemView.findViewById(R.id.report_km);
            caloriesText = (TextView) itemView.findViewById(R.id.report_calories);


        }
    }
}
